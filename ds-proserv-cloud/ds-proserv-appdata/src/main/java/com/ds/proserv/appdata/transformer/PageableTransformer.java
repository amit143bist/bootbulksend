package com.ds.proserv.appdata.transformer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import com.ds.proserv.common.domain.PageInformation;

@Component
public class PageableTransformer {

	public Pageable tranformToPageable(PageInformation pageInformation) {

		Pageable pageable = null;

		List<Order> orderList = new ArrayList<Order>();
		if (null != pageInformation && null != pageInformation.getPageSortParams()
				&& !pageInformation.getPageSortParams().isEmpty()) {

			pageInformation.getPageSortParams().forEach(sortParam -> {

				String fieldName = sortParam.getFieldName();
				String sortDirection = sortParam.getSortDirection();

				Order order = null;
				if (null == sortDirection) {

					order = Order.by(fieldName);

				} else if ("desc".equalsIgnoreCase(sortDirection) || "descending".equalsIgnoreCase(sortDirection)) {

					order = Order.desc(fieldName);
				} else if ("asc".equalsIgnoreCase(sortDirection) || "ascending".equalsIgnoreCase(sortDirection)) {

					order = Order.asc(fieldName);
				}

				orderList.add(order);
			});
		}

		if (null != orderList && !orderList.isEmpty()) {

			pageable = PageRequest.of(pageInformation.getPageNumber(), pageInformation.getRecordsPerPage(),
					Sort.by(orderList));
		} else {

			pageable = PageRequest.of(pageInformation.getPageNumber(), pageInformation.getRecordsPerPage());
		}

		return pageable;
	}

}