package com.wk.server.controller;

import com.wk.server.common.ApiResult;
import com.wk.server.common.PageResult;
import com.wk.server.entity.Item;
import com.wk.server.repository.ItemRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 内容记录接口：四种分类共用一套 CRUD。
 * 参数/JSON 格式错误由 GlobalExceptionHandler 统一兜底，本类只处理业务校验。
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

	// 分类白名单：与前端 src/menus.js 的 category 一一对应，新增分类两边同步
	private static final Set<String> CATEGORIES = Set.of("record", "notice", "task", "doc");

	private final ItemRepository itemRepository;

	public ItemController(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@GetMapping
	public ApiResult<PageResult<Item>> list(
			@RequestParam String category,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int pageSize,
		@RequestParam(required = false) String keyword) {
		String categoryError = validateCategory(category);
		if (categoryError != null) {
			return ApiResult.fail(categoryError);
		}
		if (page < 1) {
			return ApiResult.fail("页码必须大于0");
		}
		if (pageSize < 1 || pageSize > 50) {
			return ApiResult.fail("每页条数必须在1到50之间");
		}
		// 接口页码从 1 开始，数据库分页从 0 开始
		PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
		// 关键字去空格后为空则走普通分页查询
		String kw = keyword == null ? "" : keyword.trim();
		Page<Item> result = kw.isEmpty()
				? itemRepository.findByCategory(category, pageRequest)
				: itemRepository.searchByCategoryAndKeyword(category, kw, pageRequest);
		List<Item> records = result.getContent();
		return ApiResult.ok(new PageResult<>(result.getTotalElements(), page, records.size(), records));
	}

	@GetMapping("/{id}")
	public ApiResult<Item> detail(@PathVariable Long id) {
		return itemRepository.findById(id)
				.map(ApiResult::ok)
				.orElseGet(() -> ApiResult.fail("记录不存在"));
	}

	// 大屏聚合接口：一次请求返回各分类总数 + 最近 5 条，替代前端逐分类请求
	@GetMapping("/stats")
	public ApiResult<List<CategoryStats>> stats() {
		// 一次 GROUP BY 拿全部分类总数
		Map<String, Long> totals = itemRepository.countGroupByCategory().stream()
				.collect(Collectors.toMap(ItemRepository.CategoryCount::getCategory, ItemRepository.CategoryCount::getTotal));
		List<CategoryStats> stats = CATEGORIES.stream()
				.map(c -> new CategoryStats(c, totals.getOrDefault(c, 0L),
						itemRepository.findTop5ByCategoryOrderByUpdatedAtDesc(c)))
				.toList();
		return ApiResult.ok(stats);
	}

	@PostMapping
	public ApiResult<Item> create(@RequestBody ItemSaveRequest request) {
		String error = validate(request);
		if (error != null) {
			return ApiResult.fail(error);
		}
		String categoryError = validateCategory(request.category());
		if (categoryError != null) {
			return ApiResult.fail(categoryError);
		}
		Item item = new Item();
		item.setTitle(request.title().trim());
		item.setContent(request.content().trim());
		item.setCategory(request.category());
		return ApiResult.ok(itemRepository.save(item));
	}

	@PutMapping("/{id}")
	public ApiResult<Item> update(@PathVariable Long id, @RequestBody ItemSaveRequest request) {
		String error = validate(request);
		if (error != null) {
			return ApiResult.fail(error);
		}
		return itemRepository.findById(id).map(item -> {
			item.setTitle(request.title().trim());
			item.setContent(request.content().trim());
			return ApiResult.ok(itemRepository.save(item));
		}).orElseGet(() -> ApiResult.fail("记录不存在"));
	}

	@DeleteMapping("/{id}")
	public ApiResult<Void> delete(@PathVariable Long id) {
		// deleteById 一次 SQL；记录不存在时 JPA 抛 EmptyResultDataAccessException
		try {
			itemRepository.deleteById(id);
			return ApiResult.ok(null);
		} catch (EmptyResultDataAccessException e) {
			return ApiResult.fail("记录不存在");
		}
	}

	private String validateCategory(String category) {
		if (category == null || !CATEGORIES.contains(category)) {
			return "页面类型不正确";
		}
		return null;
	}

	// 新增/编辑共用的入参校验：非空 + 长度与数据库列宽一致
	private String validate(ItemSaveRequest request) {
		if (request == null || request.title() == null || request.title().isBlank()) {
			return "标题不能为空";
		}
		if (request.title().trim().length() > 100) {
			return "标题不能超过100字";
		}
		if (request.content() == null || request.content().isBlank()) {
			return "内容不能为空";
		}
		if (request.content().trim().length() > 2000) {
			return "内容不能超过2000字";
		}
		return null;
	}

	public record ItemSaveRequest(String title, String content, String category) {
	}

	public record CategoryStats(String category, long total, List<Item> items) {
	}

}
