package com.example.finance.api.planning;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.time.YearMonth;

@Tag(name = "Plannings", description = "Endpoints for managing plannings")
@RestController
@RequestMapping("/v1/plannings")
@RequiredArgsConstructor
public class PlanningResource {

    private final PlanningRepository planningRepository;
    private final PlanningService planningService;

    @Operation(summary = "Find plannings by filter")
    @GetMapping
    public ResponseEntity<Page<PlanningResponse>> findByFilter(PlanningFilter filter, Pageable pageable) {
        Page<PlanningResponse> response = planningRepository.findByFilter(filter, pageable).map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create planning")
    @PostMapping
    public ResponseEntity<PlanningResponse> create(@Valid @RequestBody PlanningRequest request) {
        var planning = planningService.create(toEntity(request));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(planning.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(planning));
    }

    @Operation(summary = "Activate planning")
    @PutMapping("/{id}/active")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        planningService.activateById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Inactivate planning")
    @PutMapping("/{id}/inactive")
    public ResponseEntity<Void> inactivate(@PathVariable Long id) {
        planningService.inactivateById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update planning")
    @PutMapping("/{id}")
    public ResponseEntity<PlanningResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody PlanningRequest request) {
        var planning = planningService.update(id, toEntity(request));
        return ResponseEntity.ok(toResponse(planning));
    }

    @Operation(summary = "Delete planning by Id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        planningService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get planning by Id")
    @GetMapping("/{id}")
    public ResponseEntity<PlanningResponse> findById(@PathVariable Long id) {
        Planning planning = planningService.findById(id);
        return ResponseEntity.ok(toResponse(planning));
    }

    private Planning toEntity(PlanningRequest request) {
        return Planning.builder()
                .amount(request.amount())
                .description(request.description())
                .dueDay(request.dueDay())
                .startAt(request.startAt().atDay(request.dueDay()))
                .endAt(request.endAt().atDay(request.dueDay()))
                .type(request.type())
                .build();
    }

    private PlanningResponse toResponse(Planning planning) {
        return new PlanningResponse(
                planning.getId(),
                planning.getDescription(),
                planning.getDueDay(),
                planning.getType(),
                planning.getActive(),
                planning.getAmount(),
                YearMonth.from(planning.getStartAt()),
                YearMonth.from(planning.getEndAt()));
    }
}
