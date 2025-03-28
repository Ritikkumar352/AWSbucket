package com.VidAWS.bucket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface videoRepo extends JpaRepository<Video,Integer> {


}
