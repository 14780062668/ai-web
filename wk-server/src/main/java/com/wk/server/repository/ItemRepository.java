package com.wk.server.repository;

import com.wk.server.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

	Page<Item> findByCategory(String category, Pageable pageable);

	// 关键字搜索：标题或内容模糊匹配，%:keyword% 由 Spring Data 自动拼通配符
	@Query("SELECT i FROM Item i WHERE i.category = :category AND (i.title LIKE %:keyword% OR i.content LIKE %:keyword%)")
	Page<Item> searchByCategoryAndKeyword(@Param("category") String category, @Param("keyword") String keyword,
			Pageable pageable);

	// 大屏：一次 GROUP BY 查出各分类总数，避免逐分类 count
	@Query("SELECT i.category AS category, COUNT(i) AS total FROM Item i GROUP BY i.category")
	List<CategoryCount> countGroupByCategory();

	// 大屏：单分类最近 5 条，走 (category, updated_at) 索引
	List<Item> findTop5ByCategoryOrderByUpdatedAtDesc(String category);

	// 分类计数投影
	interface CategoryCount {

		String getCategory();

		Long getTotal();

	}

}
