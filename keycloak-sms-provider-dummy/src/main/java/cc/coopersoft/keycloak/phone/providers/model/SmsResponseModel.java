/*
 * Copyright (C) Behin Sazan Dade Pazhoh, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Reza Tashtboland<ceo@daityc.com>, November 2019
 */

package cc.coopersoft.keycloak.phone.providers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@author : RTB 6/20/21
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsResponseModel {
    @JsonProperty(value = "Value")
    private String value;
    @JsonProperty(value = "RetStatus")
    private int status;
    @JsonProperty(value = "StrRetStatus")
    private String strStatus;
}
