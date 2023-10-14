package br.com.yannsmldevz.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.yannsmldevz.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel,HttpServletRequest request){
        Object idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        LocalDateTime currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("A data de inicio é menor que a data atual");
        }

        if(currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("A data de termino é menor que a data atual");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("A data de termino é menor que a data de inicio");
        }

        var taskCreated = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
    }

    @GetMapping("/")
    public ResponseEntity list(HttpServletRequest request){
        Object idUser = request.getAttribute("idUser");
        List<TaskModel> tasks = this.taskRepository.findByIdUser((UUID)idUser);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(
        @RequestBody TaskModel taskModel, 
        @PathVariable UUID id,
        HttpServletRequest request
    ){
        var task = this.taskRepository.findById(id).orElse(null);
        Object idUser = request.getAttribute("idUser");

        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("A tarefa não encontrada");
        }

        if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("O Usuário não possui permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
    }
}
