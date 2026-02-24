package com.jpa.market;

import com.jpa.market.constant.ItemSellStatus;
import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.QItem;
import com.jpa.market.repository.ItemRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ItemTest {

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void creteItemTest() {
        ItemFormDto itemFormDto = new ItemFormDto();

        itemFormDto.setItemName("테스트 상품");
        itemFormDto.setPrice(10000);
        itemFormDto.setStockNumber(10);
        itemFormDto.setItemDetail("상품 상세 설명");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);

        Item item = Item.createItem(itemFormDto);

        Item savedItem = itemRepository.save(item);
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getRegTime()).isNotNull();

        System.out.println(savedItem);
    }

    public void createItemList() {
        for(int i = 1; i <= 10; i++) {
            ItemFormDto itemFormDto = new ItemFormDto();

            itemFormDto.setItemName("테스트 상품" + i);
            itemFormDto.setPrice(10000 + i);
            itemFormDto.setStockNumber(10 + i);
            itemFormDto.setItemDetail("상품 상세 설명" + i);
            itemFormDto.setItemSellStatus(ItemSellStatus.SELL);

            Item item = Item.createItem(itemFormDto);

            itemRepository.save(item);
        }
    }

    @Test
    public void findByItemNameTest() {
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemName("테스트 상품3");

        // for(Item item : itemList) {
        //    System.out.println(item);
        // }

        // 컬렉션.forEach(): 컬렛션(List,Set,Map)에서 제공하는 메서드
        // itemList.forEach(item -> System.out.println(item));

        // System.out::println => item -> System.out.println(item)
        itemList.forEach(System.out::println);
    }

    @Test
    public void findByItemNameOrItemDetailTest() {
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemNameOrItemDetail("테스트 상품3", "상품 상세 설명5");

        itemList.forEach(System.out::println);
    }

    @Test
    public void findByItemDetailTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetail("1");
        itemList.forEach(System.out::println);
    }

    @Test
    public void queryDslTest() {
        this.createItemList();

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QItem qItem = QItem.item;

        // 1. JPAQuery를 만들어서 결과를 List에 저장
        JPAQuery<Item> query = queryFactory.selectFrom(qItem)
                                            .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL)
                                            .and(qItem.itemDetail.contains("설명")))
                                            .orderBy(qItem.price.desc());

        List<Item> itemList = query.fetch();

        // 2. List에 바로 저장
        List<Item> list = queryFactory.selectFrom(qItem)
                                        .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL)
                                                .and(qItem.itemDetail.contains("설명")))
                                        .orderBy(qItem.price.desc())
                                        .fetch();

        list.forEach(System.out::println);
    }
}
