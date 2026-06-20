package com.example.travel_insurance_back.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("approval_log")
public class ApprovalLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long policyId;
    private Long operatorId;
	private String action;
    private String remark;
    private LocalDateTime createdDate;
	
    
}