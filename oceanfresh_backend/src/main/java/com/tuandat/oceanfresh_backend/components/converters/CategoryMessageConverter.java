// package com.tuandat.oceanfresh_backend.components.converters;

// import java.util.Collections;

// import org.springframework.kafka.support.converter.JsonMessageConverter;
// import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
// import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
// import org.springframework.stereotype.Component;

// import com.tuandat.oceanfresh_backend.models.Category;

// @Component
// public class CategoryMessageConverter extends JsonMessageConverter {
//     public CategoryMessageConverter() {
//         super();
//         DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
//         typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
//         typeMapper.addTrustedPackages("com.tuandat.oceanfresh_backend");
//         typeMapper.setIdClassMapping(Collections.singletonMap("category", Category.class));
//         this.setTypeMapper(typeMapper);
//     }
// }
