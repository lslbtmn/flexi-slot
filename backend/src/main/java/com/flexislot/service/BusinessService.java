package com.flexislot.service;

import com.flexislot.domain.Business;
import com.flexislot.dto.business.BusinessRequest;
import com.flexislot.dto.business.BusinessResponse;
import com.flexislot.exception.ForbiddenException;
import com.flexislot.exception.ResourceNotFoundException;
import com.flexislot.repository.BusinessRepository;
import com.flexislot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.flexislot.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessResponse create(BusinessRequest request, UserPrincipal principal) {
        if (businessRepository.existsByOwnerUserId(principal.getId())) {
            throw new com.flexislot.exception.AppException("User already has a business",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        Business business = Business.builder()
                .ownerUserId(principal.getId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .location(request.getLocation())
                .serviceType(request.getServiceType())
                .operatingHours(request.getOperatingHours())
                .build();
        business = businessRepository.save(business);
        log.info("Created business {} for owner {}", business.getId(), principal.getId());
        return toResponse(business);
    }

    @Transactional(readOnly = true)
    public BusinessResponse getById(String id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + id));
        return toResponse(business);
    }

    @Transactional(readOnly = true)
    public PageResponse<BusinessResponse> getAll(Pageable pageable) {
        Page<Business> businesses = businessRepository.findAll(pageable);
        return PageResponse.<BusinessResponse>builder()
                .content(businesses.getContent().stream().map(this::toResponse).toList())
                .page(businesses.getNumber())
                .size(businesses.getSize())
                .totalElements(businesses.getTotalElements())
                .totalPages(businesses.getTotalPages())
                .first(businesses.isFirst())
                .last(businesses.isLast())
                .build();
    }

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BusinessResponse update(String id, BusinessRequest request, UserPrincipal principal) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + id));
        if (!business.getOwnerUserId().equals(principal.getId())) {
            throw new ForbiddenException("Not allowed to update this business");
        }
        business.setName(request.getName());
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setLocation(request.getLocation());
        business.setServiceType(request.getServiceType());
        business.setOperatingHours(request.getOperatingHours());
        business = businessRepository.save(business);
        log.info("Updated business {}", business.getId());
        return toResponse(business);
    }

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    public void delete(String id, UserPrincipal principal) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + id));
        if (principal.getRole() != com.flexislot.domain.enums.UserRole.ADMIN
                && !business.getOwnerUserId().equals(principal.getId())) {
            throw new ForbiddenException("Not allowed to delete this business");
        }
        businessRepository.delete(business);
        log.info("Deleted business {}", id);
    }

    @Transactional(readOnly = true)
    public BusinessResponse getByOwnerId(UserPrincipal principal) {
        Business business = businessRepository.findByOwnerUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found for current user"));
        return toResponse(business);
    }

    public String getBusinessIdForOwner(UserPrincipal principal) {
        return businessRepository.findByOwnerUserId(principal.getId())
                .map(Business::getId)
                .orElse(null);
    }

    private BusinessResponse toResponse(Business b) {
        return BusinessResponse.builder()
                .id(b.getId())
                .ownerUserId(b.getOwnerUserId())
                .name(b.getName())
                .email(b.getEmail())
                .phone(b.getPhone())
                .location(b.getLocation())
                .serviceType(b.getServiceType())
                .operatingHours(b.getOperatingHours())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
