package core.controllers;

import core.entities.Student;
import core.entities.StudentProfile;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class StudentWebAndApiController {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private CourseRepository courseRepo;

    // ---------------- WEB ----------------

    @GetMapping("/students")
    public String showAllStudents(Model model) {

        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);

        return "students";
    }

    @GetMapping("/students/new")
    public String showNewStudentForm(Model model) {

        Student student = new Student();
        StudentProfile profile = new StudentProfile();

        model.addAttribute("student", student);
        model.addAttribute("profile", profile);

        return "student-form";
    }

    @PostMapping("/students")
    public String createStudentFromForm(@ModelAttribute("student") Student student,
                                        @RequestParam("address") String address,
                                        @RequestParam("phone") String phone,
                                        @RequestParam("birthDate") String birthDate,
                                        Model model) {

        Student existingStudent = studentRepo.findByEmail(student.getEmail()).orElse(null);

        if (existingStudent != null) {
            model.addAttribute("error", "Email уже используется другим студентом");
            model.addAttribute("student", student);
            model.addAttribute("profile", new StudentProfile());
            return "student-form";
        }

        StudentProfile profile = new StudentProfile();
        profile.setAddress(address);
        profile.setPhone(phone);
        profile.setBirthDate(LocalDate.parse(birthDate));

        student.setProfile(profile);
        studentRepo.save(student);

        return "redirect:/students";
    }

    @DeleteMapping("/students/{id}")
    public String removeStudentFromWeb(@PathVariable("id") Long id) {

        studentRepo.deleteById(id);

        return "redirect:/students";
    }

    // ---------------- API ----------------

    @RestController
    @RequestMapping("/api/students")
    static class StudentApi {

        @Autowired
        private StudentRepository repo;

        @Autowired
        private CourseRepository courseRepo;

        @GetMapping
        public ResponseEntity<List<Student>> getAll() {

            List<Student> allStudents = repo.findAll();
            return ResponseEntity.ok(allStudents);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Student> getOne(@PathVariable("id") Long id) {

            Student student = repo.findById(id).orElse(null);

            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(student);
        }

        @PostMapping
        public ResponseEntity<Student> addStudent(@RequestBody Student student) {

            Student saved = repo.save(student);
            return ResponseEntity.ok(saved);
        }

        @PostMapping("/{sid}/courses/{cid}")
        public ResponseEntity<String> linkCourse(@PathVariable Long sid,
                                                 @PathVariable Long cid) {

            Student student = repo.findById(sid).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            if (courseRepo.existsById(cid)) {
                student.getCourses().add(courseRepo.findById(cid).get());
                repo.save(student);
            }

            return ResponseEntity.ok("Курс назначен");
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> remove(@PathVariable Long id) {

            boolean exists = repo.existsById(id);

            if (!exists) {
                return ResponseEntity.notFound().build();
            }

            repo.deleteById(id);
            return ResponseEntity.ok("Студент удалён");
        }
    }
}