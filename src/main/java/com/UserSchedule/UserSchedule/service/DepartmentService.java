package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.request.DepartmentCreationRequest;
import com.UserSchedule.UserSchedule.dto.response.DepartmentResponse;
import com.UserSchedule.UserSchedule.entity.Department;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.DepartmentMapper;
import com.UserSchedule.UserSchedule.repository.DepartmentRepository;
import com.UserSchedule.UserSchedule.repository.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentService {
    DepartmentRepository departmentRepository;
    DepartmentMapper departmentMapper;
    RoomRepository roomRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    public DepartmentResponse createDepartment(DepartmentCreationRequest request) {
        Optional<Department> departmentOptional = departmentRepository.findByName(request.getName());
        Department department;

        if (departmentOptional.isPresent()) {
            if (Boolean.TRUE.equals(departmentOptional.get().getIsUsed())) {
                throw new AppException(ErrorCode.DEPARTMENT_EXISTED);
            } else {
                department = departmentOptional.get();
                department.setIsUsed(true);
            }
        } else {
            department = departmentMapper.toDepartment(request);
            department.setIsUsed(true);
        }
        Department saved = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(saved);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteDepartment(Integer id) {
        Department department = departmentRepository.findByDepartmentId(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        department.setIsUsed(false);
        departmentRepository.save(department);
    }

    public List<DepartmentResponse> getDepartments() {
        return departmentMapper.toDepartmentResponseList(departmentRepository.findAllByIsUsedTrue());
    }

    public DepartmentResponse getDepartmentById(int id) {
        Department department = departmentRepository.findByDepartmentId(id)
                .filter(Department::getIsUsed)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        return departmentMapper.toDepartmentResponse(department);
    }
}
