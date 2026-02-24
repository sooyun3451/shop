package com.jpa.market.service;

import com.jpa.market.entity.Item;
import com.jpa.market.entity.ItemImg;
import com.jpa.market.repository.ItemImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemImgService {

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception {
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        if(!StringUtils.isEmpty(oriImgName)) {
            imgUrl = fileService.uploadFile("items", oriImgName, itemImgFile.getBytes());

            // 저장된 전체 경로에서 s3 key(items/uuid.jpg)만 추출
            if(imgUrl.contains(".com/")) {
                imgName = imgUrl.substring(imgUrl.lastIndexOf(".com/") + 5);
            }
        }

        itemImg.updateItemImg(imgName, oriImgName, imgUrl, itemImg.getRepImgYn());

        itemImgRepository.save(itemImg);
    }

    public void deleteItemImg(Item item) throws Exception {
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(item.getId());

        for(ItemImg itemImg : itemImgList) {

            String s3Key = itemImg.getImgName();

            // 이미지를 실제 삭제 처리
            if(!StringUtils.isEmpty(s3Key))
                fileService.deleteFile(s3Key);

            // DB에서 삭제
            itemImgRepository.delete(itemImg);
        }
        itemImgRepository.flush();
    }
}
