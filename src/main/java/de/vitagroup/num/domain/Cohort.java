package de.vitagroup.num.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;

    private String description;

    @OneToOne(mappedBy = "cohort")
    private Study study;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cohort_group_id", referencedColumnName = "id")
    private CohortGroup cohortGroup;

}
