package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.LoginDevice;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.service.LoginDeviceService;
import com.sky.service.TokenRedisService;
import com.sky.utils.IpDeviceTypeUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private TokenRedisService tokenRedisService;
    @Autowired
    private LoginDeviceService loginDeviceService;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO, HttpServletRequest httpRequest) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        // 解析设备类型
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceType = IpDeviceTypeUtil.parseDeviceType(userAgent);
        String ip = IpDeviceTypeUtil.getClientIp(httpRequest);

//        // 3. 异常登录检测
//        boolean isAbnormal = loginSecurityService.detectAbnormalLogin(user.getId(), ip);
//        if (isAbnormal) {
//            // 需要额外验证（实际项目中可能需要短信验证码）
//            log.warn("检测到异常登录, userId={}, ip={}", user.getId(), ip);
//            // return Result.error("检测到异常登录，请进行短信验证");
//        }

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        // 保存到Redis（会覆盖旧Token）
        tokenRedisService.saveUserToken(employee.getId(), deviceType, token);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("退出")
    public Result<String> logout(HttpServletRequest httpRequest) {
        log.info("员工退出");
        tokenRedisService.deleteUserToken(BaseContext.getCurrentId(), IpDeviceTypeUtil.parseDeviceType(httpRequest.getHeader("User-Agent")));
        return Result.success();
    }

    /**
     * 查看当前登录设备
     */
    @GetMapping("/devices")
    public Result getLoginDevices(@RequestAttribute("userId") Long userId) {
        List<LoginDevice> devices = loginDeviceService.getUserDevices(userId);
        return Result.success(devices);
    }

    /**
     * 踢出指定设备
     */
    @PostMapping("/kickout")
    public Result kickoutDevice(@RequestAttribute("userId") Long userId,
                                @RequestParam String deviceType) {
        loginDeviceService.kickoutDevice(userId, deviceType);
        tokenRedisService.deleteUserToken(userId, deviceType);
        return Result.success("设备已踢出");
    }

    @PostMapping()
    @ApiOperation("新增员工")
    public Result<String> add(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工:{}", employeeDTO);
        employeeService.add(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("分页查询:{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.page(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("员工状态禁用/启用")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("员工id：{}状态禁用/启用:{}", id, status);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("查询员工信息")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("查询员工信息:{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息:{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
