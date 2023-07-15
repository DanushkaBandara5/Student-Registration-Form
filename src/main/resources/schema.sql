create table if not exists student(
                                      id varchar(20) primary key ,
                                      name varchar(100) not null
);
create table if not exists picture(
                                      student_id varchar(20) primary key ,
                                      picure MEDIUMBLOB not null ,
                                      constraint fk_picture foreign key (student_id) references student(id)
);
create table if not exists attendence(
                                         id int primary key auto_increment ,
                                         status ENUM('IN','OUT') not null ,
                                         stamp DATETIME not null ,
                                         student_id varchar(20) not null ,
                                         Constraint fk_attendance foreign key (student_id) references student(id)

);
create table if not exists user(
                                   user_name varchar(50) primary key ,
                                   fill_name varchar(100) not null,
                                   password varchar(100) not null


);
