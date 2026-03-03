package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CourseWebAndApiController {

    @Autowired
    private CourseRepository courseRepo;

    // ------------------ WEB ------------------

    @GetMapping("/courses")
    public String showCoursesList(Model model) {

        List<Course> courses = courseRepo.findAll();
        model.addAttribute("courses", courses);

        return "courses";
    }

    @GetMapping("/courses/new")
    public String showNewCourseForm(Model model) {

        Course course = new Course();
        model.addAttribute("course", course);

        return "course-form";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute("course") Course course) {

        courseRepo.save(course);

        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/lessons/new")
    public String showNewLessonForm(@PathVariable("id") Long id,
                                    Model model) {

        model.addAttribute("lesson", new Lesson());
        model.addAttribute("courseId", id);

        return "lesson-form";
    }

    @PostMapping("/courses/{id}/lessons")
    public String addLessonToCourse(@PathVariable("id") Long id,
                                    @ModelAttribute("lesson") Lesson lesson) {

        Course course = courseRepo.findById(id).orElse(null);

        if (course != null) {
            lesson.setCourse(course);
            course.getLessons().add(lesson);
            courseRepo.save(course);
        }

        return "redirect:/courses";
    }

    // ------------------ API ------------------

    @RestController
    @RequestMapping("/api/courses")
    static class CourseApi {

        @Autowired
        private CourseRepository repo;

        @GetMapping
        public ResponseEntity<List<Course>> listAll() {

            List<Course> allCourses = repo.findAll();
            return ResponseEntity.ok(allCourses);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Course> getOne(@PathVariable("id") Long id) {

            Course course = repo.findById(id).orElse(null);

            if (course == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(course);
        }

        @PostMapping
        public ResponseEntity<Course> create(@RequestBody Course course) {

            Course savedCourse = repo.save(course);
            return ResponseEntity.ok(savedCourse);
        }

        @PostMapping("/{courseId}/lessons")
        public ResponseEntity<String> addLesson(@PathVariable Long courseId,
                                                @RequestBody Lesson lesson) {

            Course course = repo.findById(courseId).orElse(null);

            if (course == null) {
                return ResponseEntity.notFound().build();
            }

            lesson.setCourse(course);
            course.getLessons().add(lesson);
            repo.save(course);

            return ResponseEntity.ok("Урок добавлен");
        }
    }
}