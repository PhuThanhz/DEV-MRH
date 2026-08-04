package vn.system.app.modules.jd.jobdescriptiontask.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTask;
import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTaskItem;
import vn.system.app.modules.jd.jobdescriptiontask.domain.request.ReqJobDescriptionTaskItemDTO;
import vn.system.app.modules.jd.jobdescriptiontask.domain.request.ReqTaskDTO;
import vn.system.app.modules.jd.jobdescriptiontask.domain.response.ResJobDescriptionTaskItemDTO;
import vn.system.app.modules.jd.jobdescriptiontask.domain.response.ResTaskDTO;
import vn.system.app.modules.jd.jobdescriptiontask.repository.JobDescriptionTaskItemRepository;
import vn.system.app.modules.jd.jobdescriptiontask.repository.JobDescriptionTaskRepository;

@Service
@RequiredArgsConstructor
public class JobDescriptionTaskService {

    private final JobDescriptionTaskRepository repository;
    private final JobDescriptionTaskItemRepository itemRepository;

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

            entity = repository.save(entity);

            saveItems(entity, req.getItems());
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
                itemRepository.deleteByJobDescriptionTask_Id(old.getId());
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

            entity = repository.save(entity);

            // Xóa hết mục con cũ rồi tạo lại theo danh sách mới (đơn giản, tránh lệch orderNo)
            itemRepository.deleteByJobDescriptionTask_Id(entity.getId());
            saveItems(entity, req.getItems());
        }
    }

    private void saveItems(JobDescriptionTask task, List<ReqJobDescriptionTaskItemDTO> items) {

        if (items == null || items.isEmpty())
            return;

        List<JobDescriptionTaskItem> entities = items.stream().map(itemReq -> {
            JobDescriptionTaskItem item = new JobDescriptionTaskItem();
            item.setJobDescriptionTask(task);
            item.setOrderNo(itemReq.getOrderNo());
            item.setContent(itemReq.getContent());
            return item;
        }).collect(Collectors.toList());

        itemRepository.saveAll(entities);
    }

    /*
     * GET BY JD
     */
    public List<ResTaskDTO> getByJobDescription(Long jdId) {

        List<JobDescriptionTask> tasks = repository.findByJobDescription_IdOrderByOrderNo(jdId);

        return tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ResTaskDTO convertToDTO(JobDescriptionTask task) {
        if (task == null)
            return null;
        ResTaskDTO res = new ResTaskDTO();
        res.setId(task.getId());
        res.setOrderNo(task.getOrderNo());
        res.setTitle(task.getTitle());

        List<JobDescriptionTaskItem> items = itemRepository.findByJobDescriptionTask_IdOrderByOrderNo(task.getId());
        res.setItems(items.stream().map(this::convertItemToDTO).collect(Collectors.toList()));

        return res;
    }

    private ResJobDescriptionTaskItemDTO convertItemToDTO(JobDescriptionTaskItem item) {
        ResJobDescriptionTaskItemDTO dto = new ResJobDescriptionTaskItemDTO();
        dto.setId(item.getId());
        dto.setOrderNo(item.getOrderNo());
        dto.setContent(item.getContent());
        return dto;
    }

    /*
     * DELETE
     */
    @Transactional
    public void delete(Long id) {
        itemRepository.deleteByJobDescriptionTask_Id(id);
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
