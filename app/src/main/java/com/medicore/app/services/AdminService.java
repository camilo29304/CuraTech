package com.medicore.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medicore.app.models.Appointment;
import com.medicore.app.models.BatchStock;
import com.medicore.app.models.Employee;
import com.medicore.app.models.Medicine;
import com.medicore.app.models.Patient;
import com.medicore.app.models.Role;
import com.medicore.app.models.User;

import com.medicore.app.repository.AppointmentRepository;
import com.medicore.app.repository.BatchStockRepository;
import com.medicore.app.repository.EmployeeRepository;
import com.medicore.app.repository.MedicineRepository;
import com.medicore.app.repository.PatientRepository;
import com.medicore.app.repository.RoleRepository;
import com.medicore.app.repository.UserRepository;

@Service 
public class AdminService {
    @Autowired 
    EmployeeRepository employeeRepo;
    @Autowired 
    PatientRepository patientRepo;
    @Autowired 
    AppointmentRepository aptmRepo;
    @Autowired
    BatchStockRepository batchStockRepo;
    @Autowired
    MedicineRepository medicineRepo;
    @Autowired
    RoleRepository roleRepo;
    @Autowired
    UserRepository userRepo;

    public long getTotalEmployees(){
        return employeeRepo.count();
    }
    public long getTotalPatients(){
        return patientRepo.count();
    }
    public long getTotalAppointments(){
        return aptmRepo.findByIsAvailable(false).size();
    }
    public List<Appointment> getScheduledAppointments() {
        return aptmRepo.findByIsAvailable(false);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    public List<Employee> getEmployees() {
        return getAllEmployees();
    }

    @Transactional
    public Employee updateEmployee(int id, Employee employee) {
        return employeeRepo.findById(id).map(existing -> {
            existing.setName(employee.getName());
            existing.setLastName(employee.getLastName());
            existing.setEmail(employee.getEmail());
            existing.setDocumentNumber(employee.getDocumentNumber());
            existing.setBirthDate(employee.getBirthDate());
            if (employee.getRole() != null) {
                existing.setRole(employee.getRole());
            }
            existing.setHireDate(employee.getHireDate());
            return employeeRepo.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    public List<Employee> searchEmployees(String query, String role, String status) {
        List<Employee> employees = getAllEmployees();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        String normalizedRole = role == null ? "" : role.trim();

        return employees.stream()
            .filter(employee -> normalizedQuery.isEmpty()
                || (employee.getName() != null && employee.getName().toLowerCase().contains(normalizedQuery))
                || (employee.getLastName() != null && employee.getLastName().toLowerCase().contains(normalizedQuery))
                || (employee.getDocumentNumber() != null && employee.getDocumentNumber().contains(normalizedQuery))
                || (employee.getEmail() != null && employee.getEmail().toLowerCase().contains(normalizedQuery)))
            .filter(employee -> normalizedRole.isEmpty()
                || (employee.getRole() != null && employee.getRole().getTitleName().equalsIgnoreCase(normalizedRole)))
            .filter(employee -> status == null || status.isBlank()
                || (statusEmployee(employee).equalsIgnoreCase(status)))
            .collect(Collectors.toList());
    }

    public String statusEmployee(Employee employee) {
        if (employee == null || employee.getDocumentNumber() == null) {
            return "Inactivo";
        }
        User user = userRepo.findByDocumentNumber(employee.getDocumentNumber());
        return user == null ? "Inactivo" : "Activo";
    }

    
    @Transactional
    public Employee saveEmployee(Employee employee) {
        return registerEmployee(employee,
            employee.getRole() == null ? null : employee.getRole().getTitleName());
    }

    @Transactional
    public Employee registerEmployee(Employee employee, String roleName) {
        if (employee.getBirthDate() == null || employee.getBirthDate().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("El empleado debe tener al menos 18 años");
        }

        if (employee.getDocumentNumber() == null
            || employeeRepo.findByDocumentNumber(employee.getDocumentNumber()) != null
            || userRepo.findByDocumentNumber(employee.getDocumentNumber()) != null) {
            throw new IllegalArgumentException("Ya existe un empleado con ese documento");
        }

        Role role = roleRepo.findByTitleNameIgnoreCase(normalizeRoleName(roleName));
        if (role == null) {
            throw new IllegalArgumentException("El rol seleccionado no existe");
        }
        employee.setRole(role);
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }
        Employee saved = employeeRepo.save(employee);

        User user = new User();
        user.setDocumentNumber(saved.getDocumentNumber());
        user.setPassword("pass123");
        user.setRole(roleNameMatchesAdmin(roleName) ? "ADMIN" : "MEDICO");
        user.setSourceTableId(saved.getId());
        userRepo.save(user);
        return saved;
    }

    public Employee getEmployeeById(int id) {
        return employeeRepo.findById(id).orElse(null);
    }

    @Transactional
    public boolean deactivateEmployee(int id) {
        return employeeRepo.findById(id).map(employee -> {
            User user = userRepo.findByDocumentNumber(employee.getDocumentNumber());
            if (user != null) {
                userRepo.delete(user);
            }
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean activateEmployee(int id) {
        return employeeRepo.findById(id).map(employee -> {
            if (userRepo.findByDocumentNumber(employee.getDocumentNumber()) == null) {
                User user = new User();
                user.setDocumentNumber(employee.getDocumentNumber());
                user.setPassword("pass123");
                user.setRole(roleNameMatchesAdmin(employee.getRole()) ? "ADMIN" : "MEDICO");
                user.setSourceTableId(employee.getId());
                userRepo.save(user);
            }
            return true;
        }).orElse(false);
    }

    private boolean roleNameMatchesAdmin(Role role) {
        return role != null && role.getTitleName() != null
            && role.getTitleName().toUpperCase().contains("ADMIN");
    }

    private boolean roleNameMatchesAdmin(String roleName) {
        return roleName != null && roleName.toUpperCase().contains("ADMIN");
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }
        String normalized = roleName.trim().toUpperCase();
        if (normalized.contains("ADMIN")) return "ADMINISTRADOR";
        if (normalized.contains("RECEP")) return "RECEPCION";
        if (normalized.contains("MED")) return "MEDICO";
        return roleName.trim();
    }

    public List<User> getEligibleUsers(String query, String role) {
        List<User> users = userRepo.findAll();
        String q = query == null ? "" : query.trim().toLowerCase();
        String r = role == null ? "" : role.trim();
        return users.stream()
            .filter(user -> q.isEmpty()
                || (user.getDocumentNumber() != null && user.getDocumentNumber().contains(q)))
            .filter(user -> r.isEmpty() || user.getRole().equalsIgnoreCase(r))
            .collect(Collectors.toList());
    }

    public List<BatchStock> getAllStock() {
        return batchStockRepo.findAll();
    }

    public List<BatchStock> searchStock(String query, String category, String stockStatus) {
        List<BatchStock> stock = getAllStock();
        String q = query == null ? "" : query.trim().toLowerCase();
        return stock.stream()
            .filter(batch -> q.isEmpty()
                || (batch.getMedicine() != null
                    && ((batch.getMedicine().getName() != null && batch.getMedicine().getName().toLowerCase().contains(q))
                        || String.valueOf(batch.getMedicine().getId()).contains(q))))
            .filter(batch -> stockStatus == null || stockStatus.isBlank()
                || stockStatus(batch).equalsIgnoreCase(stockStatus))
            .collect(Collectors.toList());
    }

    public String stockStatus(BatchStock batch) {
        if (batch == null) return "Agotado";
        if (batch.getQuantity() <= 0) return "Agotado";
        if (batch.getQuantity() <= 10) return "Stock Bajo";
        return "Suficiente";
    }

    public long getActiveBatchCount() {
        return batchStockRepo.findAll().stream().filter(batch -> batch.getQuantity() > 0).count();
    }

    public long getLowStockCount() {
        return batchStockRepo.findAll().stream()
            .filter(batch -> batch.getQuantity() > 0 && batch.getQuantity() <= 10).count();
    }

    public long getExpiringSoonCount() {
        LocalDate limit = LocalDate.now().plusDays(30);
        return batchStockRepo.findAll().stream()
            .filter(batch -> batch.getExpirationDate() != null
                && !batch.getExpirationDate().isBefore(LocalDate.now())
                && !batch.getExpirationDate().isAfter(limit)).count();
    }

    @Transactional
    public Medicine registerMedicine(Medicine medicine) {
        if (medicine.getName() == null || medicine.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del medicamento es obligatorio");
        }
        if (medicineRepo.findByName(medicine.getName()) != null) {
            throw new IllegalArgumentException("El medicamento ya existe");
        }
        return medicineRepo.save(medicine);
    }

    @Transactional
    public Medicine updateMedicine(int id, Medicine medicine) {
        return medicineRepo.findById(id).map(existing -> {
            existing.setName(medicine.getName());
            existing.setQuantityPerUnit(medicine.getQuantityPerUnit());
            existing.setPriceCOP(medicine.getPriceCOP());
            return medicineRepo.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado"));
    }

    @Transactional
    public BatchStock adjustStock(int batchId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        BatchStock batch = batchStockRepo.findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Lote no encontrado");
        }
        batch.setQuantity(quantity);
        return batchStockRepo.save(batch);
    }

    @Transactional
    public boolean inactivateUser(String documentNumber) {
        User user = userRepo.findByDocumentNumber(documentNumber);
        if (user == null) return false;
        userRepo.delete(user);
        return true;
    }

    @Transactional
    public boolean activateUser(String documentNumber) {
        if (userRepo.findByDocumentNumber(documentNumber) != null) return true;
        Optional<Employee> employee = Optional.ofNullable(employeeRepo.findByDocumentNumber(documentNumber));
        if (employee.isPresent()) {
            User user = new User();
            user.setDocumentNumber(documentNumber);
            user.setPassword("pass123");
            user.setRole("MEDICO");
            user.setSourceTableId(employee.get().getId());
            userRepo.save(user);
            return true;
        }
        Optional<Patient> patient = patientRepo.findByDocumentNumber(documentNumber);
        if (patient.isPresent()) {
            User user = new User();
            user.setDocumentNumber(documentNumber);
            user.setPassword("pass123");
            user.setRole("PACIENTE");
            user.setSourceTableId(patient.get().getId());
            userRepo.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteUser(String documentNumber) {
        User user = userRepo.findByDocumentNumber(documentNumber);
        if (user == null) return false;
        userRepo.delete(user);

        Employee employee = employeeRepo.findByDocumentNumber(documentNumber);
        if (employee != null) {
            employeeRepo.delete(employee);
            return true;
        }
        Optional<Patient> patient = patientRepo.findByDocumentNumber(documentNumber);
        if (patient.isPresent()) {
            patientRepo.delete(patient.get());
            return true;
        }
        return true;
    }
}
