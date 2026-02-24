package com.jpa.market.service;

import com.jpa.market.dto.ItemAdminListDto;
import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.dto.ItemSearchDto;
import com.jpa.market.dto.MainItemDto;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.ItemImg;
import com.jpa.market.mapper.ItemMapper;
import com.jpa.market.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemMapper itemMapper;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        if(itemImgFileList == null || itemImgFileList.isEmpty() || itemImgFileList.get(0).isEmpty()) {
            throw new IllegalArgumentException("첫번째 상품 이미지는 필수입니다.");
        }
        Item item = itemMapper.dtoToEntity(itemFormDto);
        itemRepository.save(item);

        for(int i = 0; i < itemImgFileList.size(); i++) {
            MultipartFile itemImgFile = itemImgFileList.get(i);

            if(itemImgFile != null && !itemImgFile.isEmpty()) {
                ItemImg itemImg = ItemImg.builder()
                        .item(item)
                        .repImgYn(i == 0 ? "Y" : "N")
                        .build();

                itemImgService.saveItemImg(itemImg, itemImgFile);
            }
        }

        return item.getId();
    }

    // 상품 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItemDetail(Long itemId) {

        // 상품 조회
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("해당 상품을 찾을 수 없습니다"));

        // 상품의 이미지는 MapStruct가 알아서 채워줌
        return itemMapper.entityToDto(item);
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        if(itemImgFileList == null || itemImgFileList.isEmpty() || itemImgFileList.get(0).isEmpty()) {
            throw new IllegalArgumentException("첫번째 상품 이미지는 필수입니다.");
        }

        // 변경할 상품을 가져옴
        Item item = itemRepository.findById(itemFormDto.getId()).orElseThrow(() -> new EntityNotFoundException("수정하려는 상품을 찾을 수 없습니다"));

        // 상품의 기본정보를 변경
        item.updateItem(itemFormDto);

        // 기존 이미지 삭제
        itemImgService.deleteItemImg(item);

        for(int i = 0; i < itemImgFileList.size(); i++) {
            MultipartFile itemImgFile = itemImgFileList.get(i);

            if(itemImgFile != null && !itemImgFile.isEmpty()) {
                ItemImg itemImg = ItemImg.builder()
                        .item(item)
                        .repImgYn(i == 0 ? "Y" : "N")
                        .build();

                itemImgService.saveItemImg(itemImg, itemImgFile);
            }
        }

        return item.getId();
    }

    // 관리자의 상품 목록
    @Transactional(readOnly = true)
    public Page<ItemAdminListDto> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    // 메인화면의 상품 목록
    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }
}
