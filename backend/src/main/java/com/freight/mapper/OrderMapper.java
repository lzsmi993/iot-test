package com.freight.mapper;

import com.freight.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order` (user_id, total_price, warehouse_id, status) VALUES (#{userId}, #{totalPrice}, #{warehouseId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Order order);

    @Select("<script>" +
            "SELECT * FROM `order`" +
            "<where>" +
            "  <if test='status != null'>AND status = #{status}</if>" +
            "</where>" +
            " ORDER BY created_at DESC" +
            " LIMIT #{offset}, #{size}" +
            "</script>")
    List<Order> findAll(@Param("offset") int offset, @Param("size") int size, @Param("status") String status);

    @Select("<script>" +
            "SELECT COUNT(*) FROM `order`" +
            "<where>" +
            "  <if test='status != null'>AND status = #{status}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("status") String status);

    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order findById(@Param("id") Long id);

    @Update("UPDATE `order` SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
