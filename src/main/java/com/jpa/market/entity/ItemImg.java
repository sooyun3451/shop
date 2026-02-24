package com.jpa.market.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_img")
@Getter
@ToString(exclude = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder // 객체 생성용 패턴
@AllArgsConstructor
public class ItemImg extends BaseEntity {

    @Id
    @Column(name = "item_img_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String repImgYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // ItemImgㅇㅔ는 create 정적 메서드를 사용하지 않음
    // 1. Item에 종속된 매서드
    // 2. 엔티티 생성할 때 검증되어야하는 규칙이 없음
    // => create는 builder 사용할 예정
    public void updateItemImg(String imgName, String oriImgName, String imgUrl, String repImgYn) {
        this.imgName = imgName;
        this.oriImgName = oriImgName;
        this.imgUrl = imgUrl;
        this.repImgYn = repImgYn;
    }
}
