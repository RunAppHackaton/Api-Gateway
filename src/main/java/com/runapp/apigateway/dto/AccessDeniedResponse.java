package com.runapp.apigateway.dto;

import com.runapp.apigateway.utill.AccessDeniedEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AccessDeniedResponse {

    private UUID error_id;
    private Integer status_code;
    private AccessDeniedEnum error_type;
    private String openid_endpoints;
    private Map<String, String> documentation_url_list;

}
