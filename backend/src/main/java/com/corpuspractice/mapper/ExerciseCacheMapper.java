package com.corpuspractice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpuspractice.entity.ExerciseCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ExerciseCacheMapper extends BaseMapper<ExerciseCache> {

    @Select("SELECT * FROM exercise_cache WHERE corpus_ids = #{corpusIds} " +
            "AND question_type = #{questionType} " +
            "AND created_at > DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    List<ExerciseCache> findValidCache(@Param("corpusIds") String corpusIds,
                                       @Param("questionType") String questionType);
}
