package kr.co.iei.product.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import kr.co.iei.product.dto.BuyProduct;
import kr.co.iei.product.dto.ProductListRowMapper;
import kr.co.iei.product.dto.SellProductRowMapper;

@Repository
public class ProductDao {
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private ProductListRowMapper ProductListRowMapper; // 상품 목록(목록 번호, 목록 이름)
	@Autowired
	private SellProductRowMapper sellProductRowMapper; // 판매상품(번호,목록번호(참조), 가격, 이름, 이미지, url)
	@Autowired
	private BuyProduct buyProduct; // 구매한 상품(상품 번호, 판매 상품 번호(참조), 멤버 번호(참조), 구매날짜, 환불 상태, 환불 날짜)
}