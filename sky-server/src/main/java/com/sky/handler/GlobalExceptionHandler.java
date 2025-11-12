package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常：直接把文案抛给前端（code=0, msg=ex.getMessage()） */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBase(BaseException ex) {
        log.warn("[BIZ] {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /** 常见参数错误：直接抛文案 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArg(IllegalArgumentException ex) {
        log.warn("[BAD_REQUEST] {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /** JSON 体/日期格式等错误：抛更友好的提示 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleBody(HttpMessageNotReadableException ex) {
        log.warn("[REQUEST_BODY] {}", ex.getMessage());
        return Result.error("请求体格式错误或日期时间格式不正确");
    }

    /** Spring 校验：@Valid 对象参数 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("参数校验失败");
        return Result.error(msg);
    }

    /** Spring 校验：@Validated 单个参数 / Query */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst().orElse("参数校验失败");
        return Result.error(msg);
    }

    /** 绑定错误（表单/Query 映射失败） */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("参数绑定失败");
        return Result.error(msg);
    }

    /** 唯一约束/完整性约束：直接抛简洁文案 */
    @ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})
    public Result<Void> handleConflict(Exception ex) {
        String raw = ex.getMessage();
        String msg = (raw != null && raw.contains("Duplicate entry"))
                ? "数据已存在（唯一约束冲突）" : "数据冲突或完整性约束失败";
        log.warn("[CONFLICT] {}", raw);
        return Result.error(msg);
    }

    /** 兜底：把异常信息抛给前端（你要求 code=0+原始msg） */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        log.error("[SERVER_ERROR]", ex);
        String msg = ex.getMessage();
        return Result.error(msg == null || msg.isBlank() ? "服务器内部错误" : msg);
    }
}
