package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.entities.Student;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class HomeController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    // ---------------- HOME ----------------

    @GetMapping("/")
    public String home() {
        String page = "index";
        return page;
    }

    // ---------------- ASSIGN ----------------

    @GetMapping("/assign")
    public String assignPage(Model model) {

        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());

        return "assign";
    }

    @PostMapping("/assign")
    public String assignStudentToCourse(@RequestParam("studentId") Long studentId,
                                        @RequestParam("courseId") Long courseId) {

        Student student = studentRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (student == null || course == null) {
            throw new IllegalArgumentException("Студент или курс не найден");
        }

        if (!student.getCourses().contains(course)) {
            student.getCourses().add(course);
            studentRepository.save(student);
        }

        return "redirect:/students";
    }

    // ---------------- LESSONS ----------------

    @GetMapping("/courses/{courseId}/lessons/new")
    public String newLessonForm(@PathVariable("courseId") Long courseId,
                                Model model) {

        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null) {
            throw new IllegalArgumentException("Курс не найден");
        }

        Lesson lesson = new Lesson();

        model.addAttribute("lesson", lesson);
        model.addAttribute("courseId", courseId);

        return "lesson-form";
    }

    @PostMapping("/courses/{courseId}/lessons")
    public String createLesson(@PathVariable Long courseId,
                               @ModelAttribute("lesson") Lesson lesson) {

        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null) {
            throw new IllegalArgumentException("Курс не найден");
        }

        lesson.setCourse(course);
        course.getLessons().add(lesson);

        courseRepository.save(course);

        return "redirect:/courses";
    }
}