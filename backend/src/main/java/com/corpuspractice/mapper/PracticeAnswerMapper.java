package com.corpuspractice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpuspractice.entity.PracticeAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface PracticeAnswerMapper extends BaseMapper<PracticeAnswer> {

    @Select("SELECT a.*, c.subcategory FROM practice_answer a " +
            "LEFT JOIN corpus c ON a.corpus_id = c.id " +
            "WHERE a.answered_at >= #{startDate} AND a.answered_at <= #{endDate}")
    List<Map<String, Object>> selectWithCategory(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    @Select("SELECT c.id, c.chinese, c.english, c.subcategory, COUNT(a.id) as error_count " +
            "FROM practice_answer a JOIN corpus c ON a.corpus_id = c.id " +
            "WHERE a.is_correct = 0 GROUP BY c.id, c.chinese, c.english, c.subcategory " +
            "ORDER BY error_count DESC LIMIT #{limit}")
    List<Map<String, Object>> findWeakCorpus(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM practice_answer a " +
            "JOIN corpus c ON a.corpus_id = c.id " +
            "WHERE c.id = #{corpusId} AND a.is_correct = 1")
    int countCorrectByCorpusId(@Param("corpusId") Long corpusId);

    @Select("SELECT COUNT(*) FROM practice_answer a " +
            "JOIN corpus c ON a.corpus_id = c.id " +
            "WHERE c.id = #{corpusId}")
    int countTotalByCorpusId(@Param("corpusId") Long corpusId);
}
