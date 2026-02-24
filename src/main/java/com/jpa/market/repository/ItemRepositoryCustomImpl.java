package com.jpa.market.repository;

import com.jpa.market.constant.ItemSellStatus;
import com.jpa.market.dto.*;
import com.jpa.market.entity.QItem;
import com.jpa.market.entity.QItemImg;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

// custom repository를 구현한 클래스를 사용할때는 클래스명이 반드시 Impl로 끝나야함.
// 검색조건 + 페이징처리를 해서 DTO의 형태로 조회
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    // QueryDsl에서 JPQL쿼리를 만들기 위한 시작점
    // 객체생성, sql문조립
    private JPAQueryFactory queryFactory;

    // EntityManager를 초기화하기 위한 생성자
    // QueryDsl은 JPA위에서만 동작하는데, JPA는 EntityManager가 필요
    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 상품의 판매 상태에 따라 조건을 설정
    // ItemSellStatus가 있을때에만 Where절을 만들고 없으면 조건 자체를 생성하지 않음
    // BooleanExpression: SQL문의 WHERE 조건 하나를 객체로 표현한 것
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
                                                // WHERE item_sell_status = 'SELL'
    }

    // 현재날짜를 기준으로 등록일 비교
    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if(Objects.equals("all", searchDateType) || searchDateType == null)
            return null;
        else if(Objects.equals("1d", searchDateType))
            dateTime = dateTime.minusDays(1);
        else if(Objects.equals("1w", searchDateType))
            dateTime = dateTime.minusWeeks(1);
        else if(Objects.equals("1m", searchDateType))
            dateTime = dateTime.minusMonths(1);
        else if(Objects.equals("6m", searchDateType))
            dateTime = dateTime.minusMonths(6);

        return QItem.item.regTime.after(dateTime);
    }

    // 검색기준에 따라서 검색어가 포함된 자료 찾기
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if(searchQuery == null || searchQuery.isEmpty())
            return null;

        if(Objects.equals("itemName", searchBy))
            return QItem.item.itemName.contains(searchQuery);
        else if(Objects.equals("createdBy", searchBy))
            return QItem.item.createdBy.contains(searchQuery);

        return null;
    }

    // Item목록을 Dto로 조회하는 메서드
    @Override
    public Page<ItemAdminListDto> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        // QueryDsl 시작부분
        List<ItemAdminListDto> content = queryFactory
                // 필요한 자료만 조회하도록 엔티티가 아닌 dto로 조회
                .select(new QItemAdminListDto(
                        QItem.item.id,
                        QItem.item.itemName,
                        QItem.item.itemSellStatus,
                        QItem.item.createdBy,
                        QItem.item.regTime
                ))
                .from(QItem.item)
                .where(
                        // BooleanExpression으로 반환받으므로 null이면 자동으로 조건을 무시하고
                        // 조건이 있으면 AND로 결합하여 where을 형성
                        regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                )
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset()) // 페이징
                .limit(pageable.getPageSize()) // 페이징
                .fetch(); // SQL문 실행

        // 페이징 처리를 위해 전체 개수를 계산

        JPAQuery<Long> countQuery = queryFactory
                .select(QItem.item.count())
                .from(QItem.item)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                );

        // 조회 쿼리와 분리한 이유: 성능 최적화
        // 현재보고있는 페이지가 마지막 페이지인 경우
        // 전체 목롯 수가 pageSize보다 적은 경우
        // PageableExecutionUtils: Page객체를 만들어주는 유틸 클래스
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        List<MainItemDto> content = queryFactory
                .select(new QMainItemDto(
                        item.id,
                        item.itemName,
                        item.itemDetail,
                        itemImg.imgUrl,
                        item.price)
                )
                .from(itemImg)               // 더 강력한 조건을 가지고 있는 엔티티를 작성
                .join(itemImg.item, item) // 연관관계가 있으면 ON조건은 자동으로 샐성 .join(연관관계 필드, 조인된 엔티티 별칭)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNameLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(item.count())
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNameLike(itemSearchDto.getSearchQuery()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression itemNameLike(String searchQuery) {

        if(searchQuery == null || searchQuery.isEmpty())
            return null;

        return QItem.item.itemName.contains(searchQuery);
    }
}
