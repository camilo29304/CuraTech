package com.medicore.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicore.app.models.BatchStock;
import com.medicore.app.models.Medicine;

@Repository
public interface BatchStockRepository extends JpaRepository<BatchStock, Integer>{
    boolean existsByMedicineAndQuantityGreaterThan(Medicine medicine, int quantity);

    BatchStock findByMedicineAndQuantityGreaterThan(Medicine medicine, int quantity);
    BatchStock findById(int id);
}
