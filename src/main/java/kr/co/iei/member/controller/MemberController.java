package kr.co.iei.member.controller;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import kr.co.iei.member.model.dto.Member;
import kr.co.iei.member.model.dto.Title;
import kr.co.iei.member.model.service.MemberService;
import kr.co.iei.product.service.ProductService;
import kr.co.iei.utils.EmailSender;
import kr.co.iei.utils.FileUtils;

@Controller
@RequestMapping(value = "/member")
public class MemberController {
	@Autowired
	private MemberService memberService;
	@Autowired
	private EmailSender emailSender;
	@Autowired
	private ProductService productService;
	
	@Value("${file.root}")
	private String root;
	@Autowired
	private FileUtils fileUtils;//파일 업로드용
	
	@GetMapping(value="/login")
	public String login() {
		return "member/login";
	}
	@PostMapping(value="/loginin")
	public String login(Member m, HttpSession session, Model model) {
		Member member = memberService.selectOneMember(m);
		
		if(member == null) {
			model.addAttribute("title", "로그인 실패");
			model.addAttribute("msg", "아이디 또는 비밀번호를 확인하세요.");
			model.addAttribute("icon","error");
			model.addAttribute("loc","/member/login");
			return "common/msg";
		}else {
			List sp = productService.selectUseProductInfo(member); //<= 로그인 하면 top에 있는 css가 적용되게끔.. include 안되어있어서 일단 빼둠...
			if(member.getMemberLevel() == 2) {
				session.setAttribute("member", member);
				session.setAttribute("spCss", sp); // css 적용 시키기
				model.addAttribute("member", member);
				return "redirect:/member/memberPage";
			}else if(member.getMemberLevel() == 1) {
				session.setAttribute("member", member);
				session.setAttribute("spCss", sp); // css 적용 시키기
				return "redirect:/admin/adminHome";
			}else {
				session.setAttribute("member", member);
				return "redirect:/";
			}
		}
	}
	@GetMapping(value="/logout")
	public String logout(HttpSession session, Model model) {
		session.invalidate();
		model.addAttribute("title", "로그아웃");
		model.addAttribute("msg","안녕히 가세요~");
		model.addAttribute("icon","success");
		model.addAttribute("loc","/");
		return "common/msg";
	}
	@GetMapping(value="/homelist")
	public String homelist(@SessionAttribute(required=false) Member member,Model model) {
		int memberNo = 0;
		if(member != null) {
			memberNo = member.getMemberNo();
		}
		List memberList = memberService.viewAllMember(memberNo);
		model.addAttribute("memberList", memberList);
		return "common/homelist";
	}
	@GetMapping(value="/search")
	public String search(@SessionAttribute(required=false) Member member, String findFriend, Model model) {
		int memberNo = 0;
		if(member != null) {
			memberNo = member.getMemberNo();
		}
		if(findFriend.equals("")) {
			model.addAttribute("title","검색을 입력해주세요");
			model.addAttribute("icon", "error");
		}else {
			List memberList = memberService.findMember(memberNo,findFriend);
			
			model.addAttribute("memberList", memberList);
			return "/common/searchlist";				
		}
		model.addAttribute("loc","/");
		return "common/msg";
		
		
			
	}
	@GetMapping(value="/minihomepage")
		public String minihomepage() {
			return "common/minihomepage";
		}
	@GetMapping(value="/joinFrm")
	public String joinFrm() {
		return "/member/joinFrm";
	}
	@PostMapping(value="/join")
	public String join(Member m,String memberId2, String[]phone) {
		String memberId = m.getMemberId()+"@"+memberId2;
		String memberPhone = phone[0]+"-"+phone[1]+"-"+phone[2];
		m.setMemberId(memberId);
		m.setMemberPhone(memberPhone);
		int result = memberService.insertMember(m);
		if(result>0) {
			/*회원가입 성공 시 - 배경, 커서, 폰트 정리*/
			int r = memberService.joinProduct(m);
			return "redirect:/";
		}else {
			return "redirect:/member/joinFrm";
		}
	}
	@GetMapping(value="/joinFinal")
	public String joinFinal() {
		return "/member/joinFinal";
	}
	@GetMapping(value="/findPassword")
	public String findPassword() {
		return "/member/findPassword";
	}
	
	@PostMapping(value="/doubleCheckPassword")
	public String doubleCheckPassword(String id,Model model) {
		
		model.addAttribute("id", id);
		return "/member/doubleCheckPassword";
	}
	@PostMapping(value="/resetPassword")
	public String resetPassword(Member m) {
		int result = memberService.resetPassword(m);
		if(result>0) {
			m.setMemberId(m.getMemberId());
			m.setMemberPw(m.getMemberPw());
			return "redirect:/member/login";
		}else {
			return "redirect:/";			
		}
	}
	@GetMapping(value="/memberPage")
	public String memberPage(@SessionAttribute(required=false) Member member,Model model) {
		Member m = memberService.selectFriendPage(member);
		Title getTitle = memberService.getTitle(member);
		model.addAttribute("member",m);
		model.addAttribute("board",getTitle.getBoard());
		model.addAttribute("photo",getTitle.getPhoto());
		model.addAttribute("photo1",getTitle.getPhoto1());
		
		return "member/memberPage";
	}
	
	@PostMapping(value="/profile")
	public String updateProfile(MultipartFile profilePhoto, @SessionAttribute(required=false) Member member, Model model ) {
		String savePath = root+"/member/";
		String filePath = fileUtils.upload(savePath, profilePhoto);
		
		member.setProfilePhoto(filePath);
		model.addAttribute("member", member);
		
		
		int result = memberService.updateProfile(member);
		if(result>0) {
			member.setProfilePhoto(filePath);
			model.addAttribute("title", "변경 완료!");
			model.addAttribute("msg","프로필 변경되었습니다.");
			model.addAttribute("icon","success");
		}else {
			model.addAttribute("title", "변경 실패ㅠㅠ");
			model.addAttribute("msg","다시 시도해 주세요.");
			model.addAttribute("icon","error");
		}
		model.addAttribute("loc","/member/memberPage");
		return "common/msg";
	}
	
	@GetMapping(value="/friendPage")
	public String selectFriendPage(Member m, Model model) {
		Member member = memberService.selectFriendPage(m);
		Title getTitle = memberService.getTitle(member);
		List sp = productService.selectUseProductInfo(member); // css 적용
		model.addAttribute("friendMember", member);
		model.addAttribute("spCss",sp);
		model.addAttribute("board",getTitle.getBoard());
		model.addAttribute("photo",getTitle.getPhoto());
		model.addAttribute("photo1",getTitle.getPhoto1());
		return "/member/friendPage";
		
	}
	@ResponseBody
	@GetMapping(value="/ajaxCheckNickname")
	public int ajaxCheckNickname(String memberNickName) {
		Member member = memberService.selectOneMember(memberNickName);
		if(member == null) {
			return 0;
		}else {
			return 1;
		}
	}
	
	@ResponseBody
	@PostMapping(value="/sendCode")
	public String sendCode(String receiver) {
		String emailTitle = "InsideMirror 인증메일 입니다.";
		
		Random r = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<6;i++) {
			int flag = r.nextInt(3);
			if(flag == 0) {
				int randomCode = r.nextInt(10); //0~9 : r.nextInt(10); 숫자
				sb.append(randomCode);
			}else if(flag == 1) {
				char randomCode = (char)(r.nextInt(26)+65); //A~Z : r.nextInt(26)+65; 대문자 
				sb.append(randomCode);
			}else if(flag == 2) {
				char randomCode = (char)(r.nextInt(26)+97); //0~9 : r.nextInt(26)+97; 소문자
				sb.append(randomCode);
			}
		}
		String emailContent = "<h1>InsideMirror 인증코드를 확인하여 주세요.</h1>"
							+"<h3>인증번호는 [<span style='color:red; font-size:bold;'>"
							+sb.toString()
							+"</span>]입니다.</h3>";
		emailSender.sendMail(emailTitle, receiver, emailContent);
		return sb.toString();
	}
	
	@ResponseBody
	@PostMapping(value="/updateMsg")
	public int updateMsg(String profileMsg, @SessionAttribute(required=false)Member member) {
		int result = memberService.updateMsg(profileMsg, member);
		return result;
	}
	
	
	
	
	
	@GetMapping(value="/loginMsg")
	public String loginMsg(Model model){
		model.addAttribute("title", "로그인");
		model.addAttribute("msg", "로그인 후 이용가능한 서비스입니다.");
		model.addAttribute("icon","warning");
		model.addAttribute("loc","/member/login");
		return "common/msg";
	}
	
	
	
	
	
	
	
	
	/*
	 * @GetMapping(value="/updateProfileContent") public String
	 * updateProfileContent(Member m,Model model) { System.out.println("프로필 테스트"+m);
	 * int test = 1; Member member = memberService.updateProfileContent(m);
	 * model.addAttribute("member2",member); return "redirect:/member/memberPage";
	 * 
	 * }
	 */










	///사진첩
	@GetMapping(value="/photo")
	public String friendPhoto(int memberNo, Model model){
		int totalCount = memberService.getTotalCount(memberNo);
		Member friendMember = memberService.getFriendMember(memberNo);
		List sp = productService.selectUseProductInfo(friendMember);
		model.addAttribute("spCss",sp);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("memberNo", memberNo);
		model.addAttribute("friendMember", friendMember);
		return "/member/friendPhotoList";
	}//다른 사람 사진첩 조회

	@ResponseBody
	@GetMapping(value="/more")
	public List friendPhotoMore(int start, int amount, int friendMemberNo, @SessionAttribute(required = false) Member member){
		//memberNo는 친구 고유번호 session은 나
		List photoList = memberService.selectPhotoList(start, amount, friendMemberNo, member);
		return photoList;
	}//사진첩 조회
	//친구 정렬 만들기

	@ResponseBody
	@GetMapping(value="/sort")
	public List friendPhotoSort(int start, int amount, int sort, int friendMemberNo, @SessionAttribute(required = false) Member member){
		List photoList = memberService.selectFriendPhotoSort(start, amount, sort, friendMemberNo, member);
		return photoList;
	}


}
