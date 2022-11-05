// 查询列表数据
const getSetmealPage = (params,token) => {
  return $axios({
    url: '/setmeal/page',
    method: 'get',
    params,
	headers:{
		token:token
	}
  })
}

// 删除数据接口
const deleteSetmeal = (ids,token) => {
	console.log(ids)
  return $axios({
    url: '/setmeal',
    method: 'delete',
    params: { ids },
	headers:{
		token:token
	}
  })
}

// 修改数据接口
const editSetmeal = (params,token) => {
  return $axios({
    url: '/setmeal',
    method: 'put',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 新增数据接口
const addSetmeal = (params,token) => {
  return $axios({
    url: '/setmeal',
    method: 'post',
    data: { ...params },
	headers:{
		token:token
	}
  })
}

// 查询详情接口
const querySetmealById = (id,token) => {
  return $axios({
    url: `/setmeal/${id}`,
    method: 'get',
	headers:{
		token:token
	}
  })
}

// 批量起售禁售
const setmealStatusByStatus = (params,token) => {
  return $axios({
    url: `/setmeal/status/${params.status}`,
    method: 'post',
    params: { ids: params.ids },
	headers:{
		token:token
	}
  })
}
