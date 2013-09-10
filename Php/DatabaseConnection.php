<?php

class DatabaseConnection
{
	private $mySql;
	private $resultSet;
	private $currentRow;

	public function __construct ($host, $user, $password, $dbName)
	{
		$this->mySql = new mysqli ($host, $user, $password, $dbName);
		$error = $this->mySql->connect_errno;
		if ($error)
		{
			printf ("Connect failed (%d): %s\n", $error, $this->mySql->connect_error);
			exit ();
		}
		$this->currentRow = 0;
		$this->resultSet = NULL;
	}

	public function __destruct ()
	{
		$this->mySql->close ();
	}

	public function select ($tables, $columns, $whereCondition,
			$orderByColumn = NULL, $ascending = NULL)
	{
		$columnList = $this->backquote ($columns);
		$query = "SELECT $columnList ";
		$tableList = $this->backquote ($tables);
		$query .= "FROM $tableList ";
		$query .= "WHERE $whereCondition ";
		if ($orderByColumn != NULL)
		{
			$orderByColumnList = $this->backquote ($orderByColumn);
			$query .= "ORDER BY $orderByColumnList ";
			$order = $ascending ? "ASC" : "DESC";
			$query .= $order;
		}
		$resultSet = $this->performQuery ($query);
		if ($resultSet == NULL)
		{
			throw new Exception ("Result set is null");
		}
		$iterator = new DbResultIterator ($resultSet);

		return $iterator;
	}

	public function update ($table, $columnValuePairs, $whereCondition)
	{
		$query = "UPDATE `$table` ";
		$query .= "SET ";
		$numColumns = count ($columnValuePairs);
		reset ($columnValuePairs);
		for ($i = 0; $i < $numColumns - 1; ++$i)
		{
			list($column, $value) = each ($columnValuePairs);
			$query .= "`$column` = $value, ";
		}
		list($column, $value) = each ($columnValuePairs);
		$query .= " `$column` = $value ";
		$query .= "WHERE $whereCondition";
		$success = $this->performQuery ($query);

		//echo "\nUpdate--$query--\n";

		return $success;
	}

	// Returns array (success, insertId)
	public function insert ($table, $columnValues)
	{
		$query = "INSERT INTO `$table` ";
		$columns = array_keys ($columnValues);
		$quotedColumns = $this->backquote ($columns);
		$query .= "($quotedColumns)";
		$values = array_values ($columnValues);
		$query .= "VALUES (" . implode (", ", $values) . ")";

		//echo "\nINSERT --$query--\n";

		$success = $this->performQuery ($query);
		$insertId = $this->mySql->insert_id;
		$results = array ($success, $insertId);

		return $results;
	}

	public function delete ($table, $whereCondition)
	{
		$query = "DELETE FROM `$table` ";
		$query .= "WHERE $whereCondition";
		$success = $this->performQuery ($query);

		return $success;
	}

	public function performQuery ($query)
	{
		// Use real_escape_string later
		$result = $this->mySql->query ($query);
		return $result;
	}

	public function getErrorMessage()
	{
		$errorMessage = "Error $this->mySql->errno : $this->mySql->error";
		return $errorMessage;
	}

	public function close ()
	{
		$this->mySql->close ();
	}

	// Takes an array of column => value mappings and quotes them
	public function quoteValues (&$columnValuePairs)
	{
		foreach ($columnValuePairs as &$value)
		{
			$escapedValue = $this->mySql->real_escape_string ($value);
			$value = "'$escapedValue'";
		}
	}

	// Takes a single column name and quotes it
	public function quoteValue ($value)
	{
		$columnValue = array ($value);
		$this->quoteValues ($columnValue);
		return $columnValue[0];
	}

	// Takes a single column name or array of column names to backquote
	public function backquote ($columns)
	{
		if (gettype ($columns) === "array")
		{
			$columnList = "`" . implode ("`, `", $columns) . "`";
		}
		elseif (gettype ($columns === "string"))
		{
			// Only 1 string
			$columnList = $columns;
			// Don't backquote the "*"
			if ($columnList !== "*")
			{
				$columnList = "`" . $columns . "`";
			}
		}
		else
		{
			throw new Exception ("Invalid type");
		}
		return $columnList;
	}

	public function escapeString ($stringToEscape)
	{
		return $this->mySql->real_escape_string ($stringToEscape);
	}
}

class DbResultIterator implements Iterator
{
	private $currentRow;
	private $resultSet;
	private $numRows;

	public function __construct ($resultSet)
	{
		$this->currentRow = 0;
		$this->numRows = $resultSet->num_rows;
		$this->resultSet = $resultSet;
	}

	public function __destruct ()
	{
		$this->resultSet->free_result ();
	}

	public function rewind ()
	{
		$this->currentRow = 0;
		$this->resultSet->data_seek (0);
	}

	public function current ()
	{
		$row = $this->resultSet->fetch_object ();
		return $row;
	}

	public function key ()
	{
		return $this->currentRow;
	}

	public function next ()
	{
		++$this->currentRow;
	}

	public function valid ()
	{
		return $this->currentRow < $this->numRows;
	}
}

// Time and duration in format "[-]hh:[-]mm"
function addTime ($startTime, $duration)
{
	list($hours, $minutes) = explode (':', $duration);
	$endTimeString = "$startTime $hours hours, $minutes minutes";
	$endTime = date ("H:i", strtotime ($endTimeString));
	return $endTime;
}

//$dc = new DatabaseConnection ("csheadnode.cs.millersville.edu", "Geopod", "Geopod@)!)", "GeopodPoc");
//$columnValues = array ("c1" => "val1", "c2" => "val2", "c3" => "val3");
//$quotedPairs = $dc->quoteColumnValuePairs ($columnValues);
//var_dump ($quotedPairs);

