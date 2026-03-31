package com.flexislot.service;

import com.flexislot.dto.common.PageResponse;
import com.flexislot.dto.service.ServiceRequest;
import com.flexislot.dto.service.ServiceResponse;
import com.flexislot.exception.ForbiddenException;
import com.flexislot.exception.ResourceNotFoundException;
import com.flexislot.repository.BusinessRepository;
import com.flexislot.repository.ServiceRepository;
import com.flexislot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ServiceResponse create(ServiceRequest request, UserPrincipal principal) {
        String businessId = businessRepository.findByOwnerUserId(principal.getId())
                .map(com.flexislot.domain.Business::getId)
                .orElseThrow(() -> new ForbiddenException("Business not found for current user"));
        com.flexislot.domain.Service entity = com.flexislot.domain.Service.builder()
                .businessId(businessId)
                .serviceName(request.getServiceName())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .dynamicPricingEnabled(request.getDynamicPricingEnabled() != null ? request.getDynamicPricingEnabled() : true)
                .build();
        entity = serviceRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(String id) {
        com.flexislot.domain.Service entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getByBusinessId(String businessId, Pageable pageable,
            UserPrincipal principal) {
        if (principal != null && principal.getRole() == com.flexislot.domain.enums.UserRole.BUSINESS_OWNER) {
            String ownerBusinessId = businessRepository.findByOwnerUserId(principal.getId())
                    .map(com.flexislot.domain.Business::getId)
                    .orElse(null);
            if (ownerBusinessId != null && !ownerBusinessId.equals(businessId)) {
                throw new ForbiddenException("Not allowed to list services for this business");
            }
        }
        Page<com.flexislot.domain.Service> page = serviceRepository.findAllByBusinessId(businessId, pageable);
        return PageResponse.<ServiceResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ServiceResponse update(String id, ServiceRequest request, UserPrincipal principal) {
        com.flexislot.domain.Service entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        String ownerBusinessId = businessRepository.findByOwnerUserId(principal.getId())
                .map(com.flexislot.domain.Business::getId)
                .orElseThrow(() -> new ForbiddenException("Business not found"));
        if (!entity.getBusinessId().equals(ownerBusinessId)) {
            throw new ForbiddenException("Not allowed to update this service");
        }
        entity.setServiceName(request.getServiceName());
        entity.setBasePrice(request.getBasePrice());
        entity.setDurationMinutes(request.getDurationMinutes());
        if (request.getDynamicPricingEnabled() != null) {
            entity.setDynamicPricingEnabled(request.getDynamicPricingEnabled());
        }
        entity = serviceRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    public void delete(String id, UserPrincipal principal) {
        com.flexislot.domain.Service entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        if (principal.getRole() != com.flexislot.domain.enums.UserRole.ADMIN) {
            String ownerBusinessId = businessRepository.findByOwnerUserId(principal.getId())
                    .map(com.flexislot.domain.Business::getId)
                    .orElse(null);
            if (ownerBusinessId == null || !entity.getBusinessId().equals(ownerBusinessId)) {
                throw new ForbiddenException("Not allowed to delete this service");
            }
        }
        serviceRepository.delete(entity);
    }

    private ServiceResponse toResponse(com.flexislot.domain.Service s) {
        return ServiceResponse.builder()
                .id(s.getId())
                .businessId(s.getBusinessId())
                .serviceName(s.getServiceName())
                .basePrice(s.getBasePrice())
                .durationMinutes(s.getDurationMinutes())
                .dynamicPricingEnabled(s.getDynamicPricingEnabled())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
