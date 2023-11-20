package ru.zazhig.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table (name = "binary_content",schema = "public")
public class BinaryContent {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private byte[] fileAsArrayOfBytes;

}
