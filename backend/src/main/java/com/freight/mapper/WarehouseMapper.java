package com.freight.mapper;

import com.freight.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WarehouseMapper {

    @Select("SELECT id, name, location, created_at, updated_at FROM warehouse WHERE id = #{id}")
    Warehouse findById(Long id);

    @Select("SELECT id, name, location, created_at, updated_at FROM warehouse")
    List<Warehouse> findAll();
}
