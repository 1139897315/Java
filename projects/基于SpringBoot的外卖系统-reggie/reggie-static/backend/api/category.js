// 查询列表接口
const getCategoryPage = (params,token) => {
  return $axios({
    url: '/category/page',
    method: 'get',
    params,
	headers:{
		token:token
	}
  })
}

// 编辑页面反查详情接口
const queryCategoryById = (id,token) => {
  return $axios({
    url: `/category/${id}`,
    method: 'get',
	headers:{
		token:token
	}
  })
}

// 删除当前列的接口
const deleCategory = (id,token) => {
  return $axios({
    url: '/category',
    method: 'delete',
    params: { id },
	headers:{
		token:token
	}
  })
}

// 修改接口
const editCategory = (params,token) => {
  return $axios({
    url: '/category',
    method: 'put',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 新增接口
const addCategory = (params,token) => {
  return $axios({
    url: '/category',
    method: 'post',
    data: { ...params },
	headers:{
		token:token
	}
  })
}