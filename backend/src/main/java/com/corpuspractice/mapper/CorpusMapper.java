package com.corpuspractice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpuspractice.entity.Corpus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CorpusMapper extends BaseMapper<Corpus> {

    @Select("SELECT DISTINCT subcategory FROM corpus WHERE category = #{category}")
    List<String> findSubcategoriesByCategory(String category);

    @Select("<script>" +
            "SELECT * FROM corpus WHERE 1=1" +
            "<if test='category != null'> AND category = #{category}</if>" +
            " AND subcategory = #{subcategory} ORDER BY RAND() LIMIT #{limit}" +
            "</script>")
    List<Corpus> selectRandomByCategory(String category, String subcategory, int limit);
}
