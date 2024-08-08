package kr.co.iei.board.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.iei.board.model.dao.BoardDao;
import kr.co.iei.board.model.dto.Board;
import kr.co.iei.board.model.dto.BoardComment;
import kr.co.iei.board.model.dto.BoardFile;
import kr.co.iei.board.model.dto.BoardListData;

@Service
public class BoardService {

	@Autowired
	private BoardDao boardDao;

	public BoardListData selectBoardList(int reqPage) {
		int numPerPage = 10;

		int end = reqPage * numPerPage;
		int start = end - numPerPage + 1;

		List list = boardDao.selectBoardList(start, end);

		int totalCount = boardDao.selectBoardTotalCount();

		int totalPage = 0;
		if(totalCount % numPerPage == 0){
			totalPage = totalCount/numPerPage;
		}
		else{
			totalPage = totalCount/numPerPage + 1;
		}

		int pageNaviSize = 5;
		int pageNo = ((reqPage -1)/pageNaviSize) * pageNaviSize + 1;

		String pageNavi = "<ul class='page-wrap'>";

		if(pageNo != 1){
			pageNavi += "<li><a class='page-index' href='/board/list?reqPage="+(pageNo - 1)+"'><span> < </span></a></li>";
		}

		for(int i = 0; i < pageNaviSize; i++){
			pageNavi += "<li>";
			if(pageNo == reqPage){
				pageNavi += "<a class='page-index active-page' href='/board/list?reqPage="+pageNo+"'>";
			}
			else{
				pageNavi += "<a class='page-index' href='/board/list?reqPage="+pageNo+"'>";
			}
			
			pageNavi += pageNo;
			pageNavi += "</a></li>";
			pageNo++;
			
			if(pageNo > totalPage) break;
		}//for

		if(pageNo <= totalPage){
			pageNavi += "<li>";
			pageNavi += "<a class='page-index' href='/board/list?reqPage="+pageNo+"'>";
			pageNavi += "<span> > </span>";
			pageNavi += "</a></li>";
		}

		pageNavi += "</ul>";

		BoardListData bld = new BoardListData(list, pageNavi);

		return bld;
	}//리스트보기

	@Transactional
	public Board selectOneBoard(int boardNo) {
		Board board = boardDao.selectOneBoard(boardNo);
		if(board != null){
			int result = boardDao.updateReadCount(boardNo);
			//게시물 조회수 증가
			
			//댓글 조회
			List<BoardComment> commentList = boardDao.selectComment(boardNo);
			board.setCommentList(commentList);
			//답글 조회
			List reCommentList = boardDao.selectReCommentList(boardNo);
			board.setReCommentList(reCommentList);
			//답글 개수 조회
			
		}

		return board;
	}//상세보기

	@Transactional
	public int insertBoard(Board board, List<BoardFile> fileList) {
		int result = boardDao.insertBoard(board);

		if(result > 0){
			int boardNo = boardDao.selectBoardNo();

			for(BoardFile boardFile : fileList){
				boardFile.setBoardNo(boardNo);
				result += boardDao.insertBoardFile(boardFile);
			}
		}
		return result;
	}//게시판 insert

	@Transactional
	public int deleteBoard(int boardNo) {
		int result = boardDao.deleteBoard(boardNo);
		return result;
	}//게시글 삭제

	@Transactional
	public int insertBoardComment(BoardComment comment) {
		int result = boardDao.insertBoardComment(comment);
		return result;
	}//댓글 대댓글 insert

	@Transactional
	public int updateBoardComment(String commentContent, String boardCommentNo, String boardNo) {
		int result = boardDao.updateBoardComment(commentContent, boardCommentNo, boardNo);
		return result;
	}//댓글 대댓 수정

	@Transactional
	public int removeBoardComment(String boardCommentNo) {
		int result = boardDao.removeBoardComment(boardCommentNo);
		return result;
	}//댓글 대댓 삭제

	public BoardComment selectOneComment(BoardComment comment) {
		BoardComment oneComment = boardDao.selectOneComment(comment);
		return oneComment;
	}//댓글 하나 조회

	
}
