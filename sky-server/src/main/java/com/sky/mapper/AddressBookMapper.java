package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    List<AddressBook> list(AddressBook addressBook);

    void save(AddressBook addressBook);

    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    void updateById(AddressBook addressBook);

    void setDefaultByUserId(AddressBook addressBook);

    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);

    AddressBook getDefaultByUserId(AddressBook addressBook);


    void updateDefaultById(AddressBook addressBook);
}
