package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRegister {
    private String companyName;
    private String id;

    public static CompanyRegister fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        return CompanyRegister.builder()
                .companyName(company.getName())
                .id(company.getId().toHexString())
                .build();
    }
}
