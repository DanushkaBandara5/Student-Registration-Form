create table if not exists student(
                                      id varchar(20) primary key ,
                                      name varchar(100) not null,
                                      gender ENUM('MALE','FEMALE') not null,
                                      entrance timestamp not null

);
create table if not exists picture(
                                      student_id varchar(20) primary key ,
                                      picture MEDIUMBLOB not null ,
                                      constraint fk_picture foreign key (student_id) references student(id)
);

