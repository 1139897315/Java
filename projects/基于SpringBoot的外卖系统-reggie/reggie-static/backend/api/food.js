// 查询列表接口
const getDishPage = (params,token) => {
	console.log(token)
  return $axios({
    url: '/dish/page',
    method: 'get',
    params,
	headers:{
		token:token
	}
  })
}

// 删除接口
const deleteDish = (ids,token) => {
  return $axios({
    url: '/dish',
    method: 'delete', 
    params: { ids },
	headers:{
		token:token
	}
  })
}

// 修改接口
const editDish = (params,token) => {
  return $axios({
    url: '/dish',
    method: 'put',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 新增接口
const addDish = (params,token) => {
  return $axios({
    url: '/dish',
    method: 'post',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 查询详情
const queryDishById = (id,token) => {
  return $axios({
    url: `/dish/${id}`,
    method: 'get',
	headers:{
		token:token
	}
  })
}

// 获取菜品分类列表
const getCategoryList = (params,token) => {
  return $axios({
    url: '/category/list',
    method: 'get',
    params,
	headers:{
		token:token
	}
  })
}

// 查菜品列表的接口
const queryDishList = (params,token) => {
  return $axios({
    url: '/dish/list',
    method: 'get',
    params,
	headers:{
		token:token
	}
  })
}

// 文件down预览
const commonDownload = (params,token) => {
	console.log(params)
  return $axios({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
	  token:token
    },
    url: '/common/download',
    method: 'get',
    params:{
		name:params
	},
	
  })
}

// 起售停售---批量起售停售接口
const dishStatusByStatus = (params,token) => {
  return $axios({
    url: `/dish/status/${params.status}`,
    method: 'post',
    params: { ids: params.id },
	headers:{
		token:token
	}
  })
}