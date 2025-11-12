package com.sky.service;

import com.sky.constant.MessageConstant;
import com.sky.entity.User;

import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.UserMapper;
import com.sky.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class  UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 员工登录
     *
     */

    public UserVO login(User user) {
        String password = user.getPassword();
        String name = user.getName();
        //1、根据用户名查询数据库中的数据
        User user1 = userMapper.getByName(name);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user1 == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user1.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        //3、返回实体对象
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user1, userVO);
        return userVO;
    }

/*
    @Override
    public void save(userDTO userDTO) {
        User user =new User();

        BeanUtils.copyProperties(userDTO, user);

        user.setStatus(StatusConstant.DISABLE);
        user.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

//        user.setCreateTime(LocalDateTime.now());
//        user.setUpdateTime(LocalDateTime.now());
//
//        // TODO
//        user.setCreateUser(BaseContext.getCurrentId());
//        user.setUpdateUser(BaseContext.getCurrentId());


        employeeMapper.insert(user);
    }

    @Override
    public void update(userDTO userDTO) {
        //log.info("赋值前：{}",employeeService);
        User user =new User();

        BeanUtils.copyProperties(userDTO, user);
        //log.info("新增员工为：{}",user);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(user);
    }
*/
    public UserVO register(User user)   {
        User user1 = userMapper.getByName(user.getName());
        if (user1 != null) {
            throw new RuntimeException("用户名已存在");
        }
        String password = user.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        user.setPassword(password);
        int rows = userMapper.insert(user);
        Integer id=user.getId();
        log.info("用户注册成功，影响行数：{}，用户id：{}", rows, id);
        UserVO userVO = new UserVO();
        userVO.setId(id);
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
/*
    @Override
    public Boolean exists(String phone) {
        User user = userMapper.getByPhone(phone);
        return user != null;
    }
*/
}
