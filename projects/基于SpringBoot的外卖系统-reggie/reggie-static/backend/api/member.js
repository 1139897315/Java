function getMemberList (params,token) {
  return $axios({
    url: '/employee/page',
    method: 'get',
    params,
	headers:{
		token:token
	}
  })
}

// 修改---启用禁用接口
function enableOrDisableEmployee (params,token) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 新增---添加员工
function addEmployee (params,token) {
  return $axios({
    url: '/employee',
    method: 'post',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 修改---添加员工
function editEmployee (params,token) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 修改页面反查详情接口
function queryEmployeeById (id,token) {
  return $axios({
    url: `/employee/${id}`,
    method: 'get',
	headers:{
		token:token
	}
  })
}