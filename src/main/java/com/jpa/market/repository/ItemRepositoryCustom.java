package com.jpa.market.repository;

import com.jpa.market.dto.ItemAdminListDto;
import com.jpa.market.dto.ItemSearchDto;
import com.jpa.market.dto.MainItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// JPA는 인터페이스+구형클래스만 자동으로 연결 처리
// custom을 만드는 이유 1.복잡한 쿼리, 2. dto 결과 조회
public interface ItemRepositoryCustom {

    // Pageable: JPA에서 제공하는 페이징과 정렬에 대한 정보가 담겨있는 인터페이스
    // 리턴을 Page로 하면 현재 페이지의 데이터 + 전체 페이지수 + 페이징...등의 모든 정보를 담아서 리턴
    Page<ItemAdminListDto> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
}
