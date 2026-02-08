package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQuery) {
        PageHelper.startPage(setmealPageQuery.getPage(), setmealPageQuery.getPageSize());
        Page<Setmeal> p = setmealMapper.pageQuery(setmealPageQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBySetmealId(ids);
    }

    @Override
    @Transactional
    public SetmealVO getById(Long id){
        // 1. 查询套餐数据
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmealMapper.getById(id), setmealVO);
        // 2. 查询套餐菜品数据
        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        // 3. 查询分类名称，封装数据并返回
        setmealVO.setCategoryName(categoryMapper.getNameById(setmealVO.getCategoryId()));
        return setmealVO;
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
