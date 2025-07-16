package com.UserSchedule.UserSchedule.mapper;

import com.UserSchedule.UserSchedule.dto.request.DepartmentCreationRequest;
import com.UserSchedule.UserSchedule.dto.response.DepartmentResponse;
import com.UserSchedule.UserSchedule.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DepartmentMapper {

    @Mapping(target = "departmentId", ignore = true) // Nếu Department có ID
    Department toDepartment(DepartmentCreationRequest request);

    DepartmentResponse toDepartmentResponse(Department department);

    List<DepartmentResponse> toDepartmentResponseList(List<Department> departments);
}