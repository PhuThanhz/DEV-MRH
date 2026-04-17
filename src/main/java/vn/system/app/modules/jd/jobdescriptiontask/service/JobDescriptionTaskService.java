package vn.system.app.modules.jd.jobdescriptiontask.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTask;
import vn.system.app.modules.jd.jobdescriptiontask.domain.request.ReqTaskDTO;
import vn.system.app.modules.jd.jobdescriptiontask.domain.response.ResTaskDTO;
import vn.system.app.modules.jd.jobdescriptiontask.repository.JobDescriptionTaskRepository;

@Service
@RequiredArgsConstructor
public class JobDescriptionTaskService {

    private final JobDescriptionTaskRepository repository;

    /*
     * CREATE FROM JD
     */
    @Transactional
    public void createFromDTO(JobDescription jd, List<ReqTaskDTO> tasks) {

        if (tasks == null || tasks.isEmpty())
            return;

        for (ReqTaskDTO req : tasks) {

            JobDescriptionTask entity = new JobDescriptionTask();

            entity.setJobDescription(jd);
            entity.setOrderNo(req.getOrderNo());
            entity.setTitle(req.getTitle());
            entity.setContent(req.getContent());

            repository.save(entity);
        }
    }

    /*
     * UPDATE FROM JD
     */
    @Transactional
    public void updateFromDTO(JobDescription jd, List<ReqTaskDTO> tasks) {

        if (tasks == null || tasks.isEmpty())
            return;

        // Xóa các task không còn trong danh sách mới
        List<Long> keepIds = tasks.stream()
                .map(ReqTaskDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<JobDescriptionTask> existing = repository.findByJobDescription_IdOrderByOrderNo(jd.getId());

        for (JobDescriptionTask old : existing) {
            if (!keepIds.contains(old.getId())) {
                repository.delete(old);
            }
        }

        // Update hoặc tạo mới từng task
        for (ReqTaskDTO req : tasks) {

            JobDescriptionTask entity;

            if (req.getId() != null) {
                // Có id → update record cũ
                entity = repository.findById(req.getId())
                        .orElse(new JobDescriptionTask());
            } else {
                // Không có id → tạo mới
                entity = new JobDescriptionTask();
            }

            entity.setJobDescription(jd);
            entity.setOrderNo(req.getOrderNo());
            entity.setTitle(req.getTitle());
            entity.setContent(req.getContent());

            repository.save(entity);
        }
    }

    /*
     * GET BY JD
     */
    public List<ResTaskDTO> getByJobDescription(Long jdId) {

        List<JobDescriptionTask> tasks = repository.findByJobDescription_IdOrderByOrderNo(jdId);

        return tasks.stream().map(task -> {

            ResTaskDTO res = new ResTaskDTO();

            res.setId(task.getId());
            res.setOrderNo(task.getOrderNo());
            res.setTitle(task.getTitle());
            res.setContent(task.getContent());

            return res;

        }).collect(Collectors.toList());
    }

    /*
     * DELETE
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /*
     * FETCH ENTITY
     */
    public JobDescriptionTask fetchEntity(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "JobDescriptionTask không tồn tại id = " + id));
    }
}