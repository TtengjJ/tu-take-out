package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    //查询当前登录用户地址信息
    @GetMapping("/list")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> addressBookList = addressBookService.list(addressBook);
        return Result.success(addressBookList);
    }
    //新增地址
    @PostMapping
    public Result<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    //根据id查询地址
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id) {
        return Result.success(addressBookService.getById(id));
    }

    //修改地址
    @PutMapping
    public Result<AddressBook> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return Result.success(addressBook);
    }
    //删除地址
    @DeleteMapping(value = {"","/"})
    public Result delete(@RequestParam Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }
    //修改默认地址
    @PutMapping("/default")
    public Result<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    //根据用户id查询默认地址
    @GetMapping("/default")
    public Result<AddressBook> getDefault() {
        return Result.success(addressBookService.getDefaultByUserId());
    }
}
