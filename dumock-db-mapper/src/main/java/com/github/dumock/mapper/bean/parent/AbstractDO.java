package com.github.dumock.mapper.bean.parent;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * Created by jetty on 18/7/16.
 */
@NoArgsConstructor
@Data
@ToString(callSuper = true)
public class AbstractDO {

    private Long id;
    private Date createTime;
    private Date updateTime;
}
