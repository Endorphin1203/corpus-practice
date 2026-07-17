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

    @Select("SELECT * FROM corpus WHERE category = #{category} AND subcategory = #{subcategory} ORDER BY RAND() LIMIT #{limit}")
    List<Corpus> selectRandomByCategory(String category, String subcategory, int limit);
}
