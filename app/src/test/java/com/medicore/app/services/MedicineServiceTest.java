package com.medicore.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medicore.app.models.BatchStock;
import com.medicore.app.models.Medicine;
import com.medicore.app.models.Prescription;
import com.medicore.app.repository.BatchStockRepository;
import com.medicore.app.repository.MedicineRepository;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {
    @Mock 
    MedicineRepository medicineRepo;
    @Mock 
    BatchStockRepository batchRepo;
    @InjectMocks 
    MedicineService service;

    @Test 
    void setBatchStock(){
        Medicine medicine = new Medicine();
        Prescription prescription = new Prescription();
        prescription.setMedicine(medicine);
        BatchStock batch = new BatchStock();
        batch.setQuantity(10);

        when(batchRepo.findByMedicineAndQuantityGreaterThan(prescription.getMedicine(), 0)).thenReturn(batch);
        Prescription result = service.setBatchStock(prescription);

        assertEquals(9, batch.getQuantity());
        assertSame(batch, prescription.getBatchStock());
        assertSame(prescription, result);

        verify(batchRepo).findByMedicineAndQuantityGreaterThan(medicine, 0);

    }
}
