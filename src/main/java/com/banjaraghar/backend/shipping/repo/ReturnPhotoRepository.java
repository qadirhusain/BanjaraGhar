package com.banjaraghar.backend.shipping.repo;

import com.banjaraghar.backend.shipping.entity.ReturnPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReturnPhotoRepository extends JpaRepository<ReturnPhoto, UUID> {

    List<ReturnPhoto> findByReturnRequestIdInOrderBySortOrderAsc(Collection<UUID> returnRequestIds);
}